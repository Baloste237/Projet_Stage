import React, { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { Navigate } from "react-router-dom";
import { Activity, Bell } from "lucide-react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const AdminLive = () => {
  const { user } = useAuth();
  const [liveLogs, setLiveLogs] = useState<any[]>([]);

  useEffect(() => {
    // Setup WebSocket connection
    const client = new Client({
      webSocketFactory: () => new SockJS("/api/ws-admin"),
      onConnect: () => {
        console.log("Connected to WebSocket");
        client.subscribe("/topic/logs", (message) => {
          const log = JSON.parse(message.body);
          setLiveLogs((prev) => [log, ...prev].slice(0, 50)); // Keep last 50 logs
        });
      },
      onStompError: (frame) => {
        console.error("Broker reported error: " + frame.headers["message"]);
        console.error("Additional details: " + frame.body);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  if (user?.role !== "ROLE_ADMIN") {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6 animate-in fade-in duration-500 pb-12">
      <h1 className="text-3xl font-bold tracking-tight flex items-center gap-3">
        <Activity className="h-8 w-8 text-primary" />
        Live Activity Feed
      </h1>

      <div className="bg-card border rounded-md p-4 min-h-[500px]">
        {liveLogs.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-muted-foreground pt-20">
            <Bell className="h-12 w-12 mb-4 opacity-20" />
            <p>Waiting for live events...</p>
          </div>
        ) : (
          <div className="space-y-4">
            {liveLogs.map((log, index) => (
              <div key={index} className="flex items-start gap-4 p-4 border rounded-md bg-background animate-in slide-in-from-top-2">
                <div className={`p-2 rounded-full ${log.status >= 400 ? 'bg-red-100 text-red-600' : 'bg-blue-100 text-blue-600'}`}>
                  <Activity className="h-5 w-5" />
                </div>
                <div className="flex-1">
                  <div className="flex justify-between">
                    <p className="font-semibold">{log.action}</p>
                    <span className="text-xs text-muted-foreground">{new Date(log.timestamp).toLocaleTimeString()}</span>
                  </div>
                  <p className="text-sm text-muted-foreground mt-1">
                    <span className="font-medium text-foreground">{log.userId}</span> accessed <code>{log.endpoint}</code>
                  </p>
                  <p className="text-xs mt-2 text-muted-foreground">{log.details}</p>
                </div>
                <div className="text-sm font-medium">
                  Status: <span className={log.status >= 400 ? 'text-red-500' : 'text-green-500'}>{log.status}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminLive;
