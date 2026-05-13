import { UserManagement } from "@/components/UserManagement";
import { ScanConfig } from "@/components/ScanConfig";
import { ShieldAlert, Users } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { Navigate } from "react-router-dom";

const AdminDashboard = () => {
  const { user } = useAuth();

  // Redirection de sécurité côté client (le backend protège déjà l'API)
  if (user?.role !== "ROLE_ADMIN") {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500 pb-12">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-3">
            <ShieldAlert className="h-8 w-8 text-primary" />
            Panneau d'Administration
          </h1>
          <p className="text-muted-foreground mt-2 text-lg">
            Gérez les accès utilisateurs et configurez les paramètres globaux du scanner.
          </p>
        </div>
      </div>

      <div className="grid gap-8">
        <section id="users">
          <div className="mb-4">
            <h2 className="text-xl font-semibold flex items-center gap-2 border-b pb-2">
              <Users className="h-5 w-5" />
              Utilisateurs
            </h2>
          </div>
          <UserManagement />
        </section>

        <section id="config">
          <div className="mb-4">
            <h2 className="text-xl font-semibold flex items-center gap-2 border-b pb-2">
              <ShieldAlert className="h-5 w-5" />
              Configuration Système
            </h2>
          </div>
          <ScanConfig />
        </section>
      </div>
    </div>
  );
};

export default AdminDashboard;
