// Import des icônes depuis lucide-react pour les éléments de navigation et d'interface
import {
  LayoutDashboard, // Icône pour le tableau de bord
  Globe, // Icône pour le scan web
  Smartphone, // Icône pour le scan mobile
  ShieldAlert, // Icône pour les vulnérabilités
  FileBarChart, // Icône pour les rapports
  BarChart3, // Icône pour les statistiques
  Settings, // Icône pour les paramètres
  LogOut, // Icône pour la déconnexion
  Shield, // Icône pour le logo de l'application
  Moon, // Icône pour le mode sombre
  Sun, // Icône pour le mode clair
  ShieldAlert as AdminIcon, // Alias for admin
  Activity,
  FileText,
} from "lucide-react";

// Import du composant NavLink personnalisé
import { NavLink } from "@/components/NavLink";

// Import du hook useLocation de react-router-dom pour obtenir l'emplacement actuel
import { useLocation } from "react-router-dom";

// Import du hook useAuth du contexte d'authentification
import { useAuth } from "@/context/AuthContext";

// Import du hook useTheme du contexte de thème
import { useTheme } from "@/context/ThemeContext";

// Import des composants UI de la sidebar
import {
  Sidebar, // Composant principal de la sidebar
  SidebarContent, // Contenu de la sidebar
  SidebarGroup, // Groupe dans la sidebar
  SidebarGroupContent, // Contenu du groupe
  SidebarGroupLabel, // Étiquette du groupe
  SidebarMenu, // Menu de la sidebar
  SidebarMenuButton, // Bouton du menu
  SidebarMenuItem, // Élément du menu
  SidebarFooter, // Pied de page de la sidebar
  SidebarHeader, // En-tête de la sidebar
  useSidebar, // Hook pour gérer l'état de la sidebar
} from "@/components/ui/sidebar";

// Définition des éléments principaux du menu de navigation
const mainItems = [
  { title: "Dashboard", url: "/", icon: LayoutDashboard }, // Élément pour le tableau de bord
  { title: "Scan Web", url: "/scan-web", icon: Globe }, // Élément pour le scan web
  { title: "Scan Mobile", url: "/scan-mobile", icon: Smartphone }, // Élément pour le scan mobile
  { title: "Vulnérabilités", url: "/vulnerabilities", icon: ShieldAlert }, // Élément pour les vulnérabilités
  { title: "Rapports", url: "/reports", icon: FileBarChart }, // Élément pour les rapports
  { title: "Statistiques", url: "/statistics", icon: BarChart3 }, // Élément pour les statistiques
  { title: "Paramètres", url: "/settings", icon: Settings }, // Élément pour les paramètres
];

// Définition du composant fonctionnel AppSidebar
export function AppSidebar() {
  // Utilisation du hook useSidebar pour obtenir l'état de la sidebar
  const { state } = useSidebar();
  // Vérification si la sidebar est réduite
  const collapsed = state === "collapsed";
  // Utilisation du hook useLocation pour obtenir l'emplacement actuel
  const location = useLocation();
  // Utilisation du hook useAuth pour obtenir les fonctions de logout et l'utilisateur
  const { logout, user } = useAuth();
  // Utilisation du hook useTheme pour obtenir le thème et la fonction de basculement
  const { theme, toggleTheme } = useTheme();

  // Retour du JSX pour rendre la sidebar
  return (
    <Sidebar collapsible="icon" className="african-pattern border-r border-sidebar-border">
      <SidebarHeader className="border-b border-sidebar-border p-4">
        <div className="flex items-center gap-2">
          <Shield className="h-7 w-7 text-sidebar-primary shrink-0" />
          {!collapsed && (
            <span className="text-lg font-bold text-sidebar-primary font-heading tracking-tight">
              AISecureScan
            </span>
          )}
        </div>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel className="text-sidebar-foreground/50 text-xs uppercase tracking-wider">
            Navigation
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {mainItems.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink
                      to={item.url}
                      end={item.url === "/"}
                      className="hover:bg-sidebar-accent/50 transition-colors"
                      activeClassName="bg-sidebar-accent text-sidebar-accent-foreground font-medium"
                    >
                      <item.icon className="h-4 w-4 shrink-0" />
                      {!collapsed && <span>{item.title}</span>}
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
              
              {user?.role === "ROLE_ADMIN" && (
                <>
                  <SidebarMenuItem>
                    <SidebarMenuButton asChild>
                      <NavLink
                        to="/admin"
                        className="hover:bg-sidebar-accent/50 transition-colors mt-4 text-amber-600 dark:text-amber-500"
                        activeClassName="bg-sidebar-accent text-amber-700 dark:text-amber-400 font-medium"
                      >
                        <AdminIcon className="h-4 w-4 shrink-0" />
                        {!collapsed && <span>Administration</span>}
                      </NavLink>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                  <SidebarMenuItem>
                    <SidebarMenuButton asChild>
                      <NavLink
                        to="/admin/dashboard"
                        className="hover:bg-sidebar-accent/50 transition-colors text-amber-600 dark:text-amber-500"
                        activeClassName="bg-sidebar-accent text-amber-700 dark:text-amber-400 font-medium"
                      >
                        <Activity className="h-4 w-4 shrink-0" />
                        {!collapsed && <span>Admin Dashboard</span>}
                      </NavLink>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                  <SidebarMenuItem>
                    <SidebarMenuButton asChild>
                      <NavLink
                        to="/admin/logs"
                        className="hover:bg-sidebar-accent/50 transition-colors text-amber-600 dark:text-amber-500"
                        activeClassName="bg-sidebar-accent text-amber-700 dark:text-amber-400 font-medium"
                      >
                        <FileText className="h-4 w-4 shrink-0" />
                        {!collapsed && <span>Audit Logs</span>}
                      </NavLink>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                  <SidebarMenuItem>
                    <SidebarMenuButton asChild>
                      <NavLink
                        to="/admin/live"
                        className="hover:bg-sidebar-accent/50 transition-colors text-amber-600 dark:text-amber-500"
                        activeClassName="bg-sidebar-accent text-amber-700 dark:text-amber-400 font-medium"
                      >
                        <Activity className="h-4 w-4 shrink-0 text-red-500" />
                        {!collapsed && <span>Live Activity</span>}
                      </NavLink>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                </>
              )}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter className="border-t border-sidebar-border p-3 space-y-2">
        <button
          onClick={toggleTheme}
          className="flex items-center gap-2 w-full px-2 py-1.5 rounded-md text-sm text-sidebar-foreground hover:bg-sidebar-accent/50 transition-colors"
        >
          {theme === "dark" ? <Sun className="h-4 w-4 shrink-0" /> : <Moon className="h-4 w-4 shrink-0" />}
          {!collapsed && <span>{theme === "dark" ? "Mode clair" : "Mode sombre"}</span>}
        </button>
        {user && (
          <div className="flex items-center gap-2 px-2">
            <div className="h-7 w-7 rounded-full bg-sidebar-primary/20 flex items-center justify-center text-sidebar-primary text-xs font-bold shrink-0">
              {user.name[0]?.toUpperCase()}
            </div>
            {!collapsed && (
              <div className="flex-1 min-w-0">
                <p className="text-xs font-medium text-sidebar-foreground truncate">{user.name}</p>
                <p className="text-[10px] text-sidebar-foreground/50 truncate">{user.role}</p>
              </div>
            )}
          </div>
        )}
        <button
          onClick={logout}
          className="flex items-center gap-2 w-full px-2 py-1.5 rounded-md text-sm text-destructive hover:bg-destructive/10 transition-colors"
        >
          <LogOut className="h-4 w-4 shrink-0" />
          {!collapsed && <span>Déconnexion</span>}
        </button>
      </SidebarFooter>
    </Sidebar>
  );
}
