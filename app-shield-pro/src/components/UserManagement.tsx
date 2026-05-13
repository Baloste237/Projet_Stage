import { useEffect, useState } from "react";
import { fetchUsers, toggleUserStatus, changeUserRole, User } from "@/services/adminService";
import { toast } from "sonner";
import { Shield, User as UserIcon, Power, PowerOff } from "lucide-react";

export const UserManagement = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const data = await fetchUsers();
      setUsers(data);
    } catch (error: any) {
      toast.error(error.message || "Impossible de charger les utilisateurs");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleToggleStatus = async (user: User) => {
    try {
      const updatedUser = await toggleUserStatus(user.id);
      setUsers((prev) => prev.map((u) => (u.id === user.id ? updatedUser : u)));
      toast.success(`Le statut de ${user.userName} a été mis à jour`);
    } catch (error: any) {
      toast.error(error.message || "Erreur de mise à jour");
    }
  };

  const handleChangeRole = async (user: User, newRole: string) => {
    try {
      const updatedUser = await changeUserRole(user.id, newRole);
      setUsers((prev) => prev.map((u) => (u.id === user.id ? updatedUser : u)));
      toast.success(`Le rôle de ${user.userName} est maintenant ${newRole}`);
    } catch (error: any) {
      toast.error(error.message || "Erreur lors du changement de rôle");
    }
  };

  if (loading) {
    return <div className="text-center py-8 text-muted-foreground animate-pulse">Chargement des utilisateurs...</div>;
  }

  return (
    <div className="bg-card text-card-foreground rounded-xl border shadow-sm overflow-hidden">
      <div className="p-6 border-b border-border bg-muted/40 flex justify-between items-center">
        <div>
          <h3 className="text-lg font-semibold flex items-center gap-2">
            <UserIcon className="h-5 w-5 text-primary" />
            Gestion des Utilisateurs
          </h3>
          <p className="text-sm text-muted-foreground">Gérez les accès et rôles de votre équipe</p>
        </div>
      </div>
      
      <div className="overflow-x-auto">
        <table className="w-full text-sm text-left">
          <thead className="text-xs uppercase bg-muted/50 text-muted-foreground">
            <tr>
              <th className="px-6 py-3">Utilisateur</th>
              <th className="px-6 py-3">Email</th>
              <th className="px-6 py-3">Rôle</th>
              <th className="px-6 py-3">Statut</th>
              <th className="px-6 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className="border-b last:border-0 hover:bg-muted/30 transition-colors">
                <td className="px-6 py-4 font-medium">{user.userName}</td>
                <td className="px-6 py-4 text-muted-foreground">{user.email}</td>
                <td className="px-6 py-4">
                  <div className="flex items-center gap-2">
                    {user.role === "ROLE_ADMIN" ? <Shield className="h-4 w-4 text-amber-500" /> : <UserIcon className="h-4 w-4 text-blue-500" />}
                    <select
                      className="bg-transparent border border-border text-sm rounded focus:ring-primary focus:border-primary px-2 py-1 outline-none cursor-pointer"
                      value={user.role}
                      onChange={(e) => handleChangeRole(user, e.target.value)}
                    >
                      <option value="ROLE_ANALYSTE">Analyste</option>
                      <option value="ROLE_ADMIN">Admin</option>
                    </select>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className={`px-2.5 py-1 text-xs font-semibold rounded-full ${user.enabled ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400" : "bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"}`}>
                    {user.enabled ? "Actif" : "Inactif"}
                  </span>
                </td>
                <td className="px-6 py-4 text-right">
                  <button
                    onClick={() => handleToggleStatus(user)}
                    className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-primary ${
                      user.enabled
                        ? "bg-red-50 text-red-600 hover:bg-red-100 dark:bg-red-900/20 dark:hover:bg-red-900/40"
                        : "bg-green-50 text-green-600 hover:bg-green-100 dark:bg-green-900/20 dark:hover:bg-green-900/40"
                    }`}
                  >
                    {user.enabled ? (
                      <><PowerOff className="h-4 w-4" /> Désactiver</>
                    ) : (
                      <><Power className="h-4 w-4" /> Activer</>
                    )}
                  </button>
                </td>
              </tr>
            ))}
            {users.length === 0 && (
              <tr>
                <td colSpan={5} className="px-6 py-8 text-center text-muted-foreground">
                  Aucun utilisateur trouvé.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
