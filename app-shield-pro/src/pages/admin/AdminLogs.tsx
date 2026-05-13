import React, { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navigate } from "react-router-dom";
import { FileText, Download, Search } from "lucide-react";

const AdminLogs = () => {
  const { user } = useAuth();
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filterAction, setFilterAction] = useState("");

  const fetchLogs = () => {
    fetch(`/api/admin/logs?page=${page}&size=10${filterAction ? `&action=${filterAction}` : ''}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem("vulnscan-token")}` }
    })
      .then(res => res.json())
      .then(data => {
        if (data.content) {
          setLogs(data.content);
          setTotalPages(data.totalPages);
        }
      })
      .catch(console.error);
  };

  useEffect(() => {
    fetchLogs();
  }, [page, filterAction]);

  if (user?.role !== "ROLE_ADMIN") {
    return <Navigate to="/" replace />;
  }

  const exportCsv = () => {
    const csvContent = "data:text/csv;charset=utf-8," 
      + "Date,User,Action,Endpoint,Status\n"
      + logs.map(l => `${l.timestamp},${l.userId},${l.action},${l.endpoint},${l.status}`).join("\n");
    
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "audit_logs.csv");
    document.body.appendChild(link);
    link.click();
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-in fade-in duration-500 pb-12">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-3">
          <FileText className="h-8 w-8 text-primary" />
          Audit Logs
        </h1>
        <button onClick={exportCsv} className="flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2 rounded-md hover:bg-primary/90">
          <Download className="h-4 w-4" /> Export CSV
        </button>
      </div>

      <div className="flex gap-4 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input 
            type="text" 
            placeholder="Filter by action..." 
            className="w-full pl-9 pr-4 py-2 border rounded-md"
            value={filterAction}
            onChange={(e) => { setFilterAction(e.target.value); setPage(0); }}
          />
        </div>
      </div>

      <div className="border rounded-md overflow-x-auto">
        <table className="w-full text-sm text-left">
          <thead className="bg-muted text-muted-foreground border-b">
            <tr>
              <th className="px-4 py-3">Date</th>
              <th className="px-4 py-3">User</th>
              <th className="px-4 py-3">Action</th>
              <th className="px-4 py-3">Endpoint</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Level</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log: any) => (
              <tr key={log.id} className="border-b hover:bg-muted/50">
                <td className="px-4 py-3">{new Date(log.timestamp).toLocaleString()}</td>
                <td className="px-4 py-3">{log.userId}</td>
                <td className="px-4 py-3 font-medium">{log.action}</td>
                <td className="px-4 py-3 text-muted-foreground">{log.endpoint}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 rounded-full text-xs ${log.status >= 400 ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'}`}>
                    {log.status}
                  </span>
                </td>
                <td className="px-4 py-3">{log.logLevel}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between">
        <button 
          disabled={page === 0} 
          onClick={() => setPage(p => p - 1)}
          className="px-4 py-2 border rounded-md disabled:opacity-50"
        >
          Previous
        </button>
        <span className="text-sm text-muted-foreground">Page {page + 1} of {totalPages || 1}</span>
        <button 
          disabled={page >= totalPages - 1} 
          onClick={() => setPage(p => p + 1)}
          className="px-4 py-2 border rounded-md disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default AdminLogs;
