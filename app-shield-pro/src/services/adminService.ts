export const getAuthHeaders = () => {
  const token = localStorage.getItem("vulnscan-token");
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

export interface User {
  id: number;
  userName: string;
  email: string;
  role: string;
  enabled: boolean;
}

export const fetchUsers = async (): Promise<User[]> => {
  const res = await fetch("/api/admin/users", {
    method: "GET",
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error("Erreur lors de la récupération des utilisateurs");
  return res.json();
};

export const toggleUserStatus = async (id: number): Promise<User> => {
  const res = await fetch(`/api/admin/users/${id}/toggle-status`, {
    method: "PATCH",
    headers: getAuthHeaders(),
  });
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(errText || "Erreur lors de la modification du statut");
  }
  return res.json();
};

export const changeUserRole = async (id: number, role: string): Promise<User> => {
  const res = await fetch(`/api/admin/users/${id}/role`, {
    method: "PATCH",
    headers: getAuthHeaders(),
    body: JSON.stringify({ role }),
  });
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(errText || "Erreur lors de la modification du rôle");
  }
  return res.json();
};
