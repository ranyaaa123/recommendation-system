"use client";

import { useCallback, useEffect, useState } from "react";
import { AppShell } from "@/components/app-shell";
import { ConfirmDialog, type ConfirmDialogConfig } from "@/components/confirm-dialog";
import { UserCard } from "@/components/user-card";
import { api } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import type { UserSummary } from "@/lib/types";

export default function CreatorsPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [following, setFollowing] = useState<Set<string>>(new Set());
  const [uploadCounts, setUploadCounts] = useState<Record<string, number>>({});
  const [pending, setPending] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [confirm, setConfirm] = useState<ConfirmDialogConfig | null>(null);

  useEffect(() => {
    let active = true;
    setLoading(true);

    api.getUsers({ size: 48 })
      .then((response) => {
        if (!active) {
          return;
        }
        setUsers(response.users);

        // Upload count per creator comes from video-service; totalElements is the
        // full count regardless of page size.
        response.users.forEach((summary) => {
          api.getUserVideos(summary.userId)
            .then((videos) => {
              if (active) {
                setUploadCounts((prev) => ({ ...prev, [summary.userId]: videos.totalElements }));
              }
            })
            .catch(() => undefined);
        });
      })
      .catch(() => {
        if (active) {
          setUsers([]);
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    if (user?.userId) {
      api.getFollowing(user.userId)
        .then((ids) => {
          if (active) {
            setFollowing(new Set(ids));
          }
        })
        .catch(() => undefined);
    }

    return () => {
      active = false;
    };
  }, [user?.userId]);

  const applyToggle = useCallback((targetId: string, isFollowing: boolean) => {
    setPending((prev) => new Set(prev).add(targetId));
    const action = isFollowing ? api.unfollowUser(targetId) : api.followUser(targetId);

    action
      .then(() => {
        setFollowing((prev) => {
          const next = new Set(prev);
          if (isFollowing) {
            next.delete(targetId);
          } else {
            next.add(targetId);
          }
          return next;
        });
      })
      .catch(() => undefined)
      .finally(() => {
        setPending((prev) => {
          const next = new Set(prev);
          next.delete(targetId);
          return next;
        });
      });
  }, []);

  const requestToggleFollow = useCallback((targetId: string) => {
    const isFollowing = following.has(targetId);
    const creator = users.find((candidate) => candidate.userId === targetId);
    const name = creator?.displayName || creator?.username || "this creator";
    setConfirm({
      title: isFollowing ? "Unfollow creator" : "Follow creator",
      message: isFollowing
        ? `Stop following ${name}? Their activity will no longer influence your recommendations.`
        : `Follow ${name}? What they watch will start shaping your recommendations.`,
      confirmLabel: isFollowing ? "Unfollow" : "Follow",
      variant: isFollowing ? "danger" : "default",
      onConfirm: () => applyToggle(targetId, isFollowing),
    });
  }, [following, users, applyToggle]);

  return (
    <AppShell title="Browse creators" eyebrow="Community">
      {loading ? (
        <div className="rounded-2xl bg-white p-8 text-sm font-bold text-slate-500 shadow-sm">Loading creators...</div>
      ) : users.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-sm font-semibold text-slate-500">
          No other creators to show yet.
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-4">
          {users.map((summary) => (
            <UserCard
              key={summary.userId}
              user={summary}
              uploadCount={uploadCounts[summary.userId]}
              isFollowing={following.has(summary.userId)}
              pending={pending.has(summary.userId)}
              onToggleFollow={requestToggleFollow}
            />
          ))}
        </div>
      )}
      <ConfirmDialog config={confirm} onClose={() => setConfirm(null)} />
    </AppShell>
  );
}
