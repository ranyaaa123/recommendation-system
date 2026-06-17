"use client";

import { formatVideoCategoryLabel } from "@/lib/video-categories";
import type { UserSummary } from "@/lib/types";

function computeInitials(displayName?: string | null, username?: string | null) {
  const source = (displayName || username || "").trim();
  if (!source) {
    return "?";
  }
  const words = source.split(/\s+/).filter(Boolean);
  if (words.length === 1) {
    return words[0].slice(0, 2).toUpperCase();
  }
  return (words[0][0] + words[words.length - 1][0]).toUpperCase();
}

export function UserCard({
  user,
  uploadCount,
  isFollowing,
  pending,
  onToggleFollow,
}: {
  user: UserSummary;
  uploadCount?: number;
  isFollowing: boolean;
  pending: boolean;
  onToggleFollow: (userId: string) => void;
}) {
  const initials = computeInitials(user.displayName, user.username);
  const categories = user.categories.map((category) => formatVideoCategoryLabel(category)).slice(0, 3);

  return (
    <div className="flex flex-col rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between">
        <div className="grid h-14 w-14 place-items-center rounded-lg bg-slate-950 text-lg font-black text-white">
          {initials}
        </div>
        <span className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-500">
          <span className={`h-2.5 w-2.5 rounded-full ${user.isOnline ? "bg-emerald-500" : "bg-slate-300"}`} />
          {user.isOnline ? "Online" : "Offline"}
        </span>
      </div>

      <h3 className="mt-3 text-lg font-black text-slate-950">{user.displayName || user.username}</h3>
      <p className="text-xs text-slate-500">@{user.username}</p>

      <div className="mt-3 rounded-lg bg-slate-50 p-3">
        <p className="text-xl font-black text-slate-950">{uploadCount ?? "—"}</p>
        <p className="text-xs text-slate-500">Videos uploaded</p>
      </div>

      <div className="mt-3 flex flex-wrap gap-2">
        {categories.length > 0 ? (
          categories.map((category) => (
            <span key={category} className="rounded-lg bg-teal-50 px-2.5 py-1 text-xs font-bold text-teal-800">
              {category}
            </span>
          ))
        ) : (
          <span className="rounded-lg bg-slate-100 px-2.5 py-1 text-xs font-bold text-slate-500">No categories yet</span>
        )}
      </div>

      <button
        type="button"
        disabled={pending}
        onClick={() => onToggleFollow(user.userId)}
        className={`pressable mt-4 w-full rounded-lg px-3 py-2 text-sm font-bold transition disabled:opacity-60 ${
          isFollowing
            ? "border border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
            : "bg-teal-600 text-white hover:bg-teal-700"
        }`}
      >
        {isFollowing ? "Following" : "Follow"}
      </button>
    </div>
  );
}
