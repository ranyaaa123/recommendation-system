"use client";

import { useEffect, useState } from "react";
import { AdminShell } from "@/components/admin-shell";
import { api } from "@/lib/api";

export default function AdminDashboardPage() {
  const [exported, setExported] = useState(false);
  const [metrics, setMetrics] = useState([
    { label: "Users", value: "-", detail: "Loading" },
    { label: "Active users", value: "-", detail: "Loading" },
    { label: "Videos", value: "-", detail: "Loading" },
    { label: "Pending", value: "-", detail: "Loading" },
    { label: "Views", value: "-", detail: "Loading" },
  ]);
  const [notice, setNotice] = useState("");

  useEffect(() => {
    Promise.all([api.getAdminUserDashboard(), api.getAdminVideoDashboard()])
      .then(([users, videos]) => {
        setMetrics([
          { label: "Users", value: String(users.totalUsers), detail: `+${users.newUsersLast7Days} this week` },
          { label: "Active users", value: String(users.activeUsers), detail: `${users.bannedUsers} banned` },
          { label: "Videos", value: String(videos.totalVideos), detail: `${videos.publicVideos} public` },
          { label: "Pending", value: String(videos.pendingReviewVideos), detail: "Needs review" },
          { label: "Views", value: String(videos.totalViews), detail: `${videos.uploadsLast7Days} uploads this week` },
        ]);
      })
      .catch(() => setNotice("Admin data unavailable. Check the services and credentials."));
  }, []);

  const exportReport = () => {
    setExported(true);
    window.setTimeout(() => setExported(false), 1400);
  };

  return (
    <AdminShell title="Dashboard">
      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        {metrics.map((metric) => (
          <div key={metric.label} className="magnetic-card rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <p className="text-xs font-bold uppercase tracking-[0.18em] text-slate-500">{metric.label}</p>
            <p className="mt-3 text-3xl font-black text-slate-950">{metric.value}</p>
            <p className="mt-1 text-sm text-slate-500">{metric.detail}</p>
          </div>
        ))}
      </section>
      {notice ? <div className="mt-4 rounded-lg bg-orange-50 px-3 py-2 text-sm font-bold text-orange-800">{notice}</div> : null}

      <section className="mt-6">
        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-xl font-black text-slate-950">Recent activity</h2>
              <p className="mt-1 text-sm text-slate-500">Operational events across services.</p>
            </div>
            <button
              onClick={exportReport}
              className="pressable rounded-lg border border-slate-200 px-3 py-2 text-sm font-bold text-slate-700 hover:bg-slate-50"
            >
              {exported ? "Exported" : "Export"}
            </button>
          </div>
          {exported ? (
            <div className="reaction-burst mt-4 rounded-lg bg-teal-50 px-3 py-2 text-sm font-bold text-teal-800">
              Dashboard report prepared locally.
            </div>
          ) : null}
          <div className="mt-5 rounded-xl border border-dashed border-slate-300 bg-slate-50 p-8 text-center">
            <h3 className="text-lg font-black text-slate-950">No live activity stream yet</h3>
            <p className="mt-2 text-sm text-slate-500">
              This dashboard now shows only real aggregate metrics. Add an audit/event endpoint before listing individual activity rows.
            </p>
          </div>
        </div>
      </section>
    </AdminShell>
  );
}
