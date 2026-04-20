import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { Shield, Eye, EyeOff } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { toast } from "sonner";

export default function Register() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPw, setConfirmPw] = useState("");
  const [role, setRole] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPw) {
      toast.error("Les mots de passe ne correspondent pas");
      return;
    }
    if (!role) {
      toast.error("Veuillez sélectionner un rôle");
      return;
    }
    
    setIsLoading(true);
    try {
      const success = await register(name, email, password, role);
      if (success) {
        toast.success("Compte créé avec succès !");
        navigate("/");
      } else {
        toast.error("Erreur lors de la création du compte");
      }
    } catch (e) {
      toast.error("Erreur inattendue");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md border-border/50">
        <CardHeader className="text-center space-y-4 pb-2">
          {/* <div className="mx-auto p-3 rounded-xl bg-primary/10 w-fit">
            <Shield className="h-8 w-8 text-primary" />
          </div> */}

          <div className="mx-auto w-fit">
            <img 
              src="/logo_AIsecure2-removebg-preview.png" 
              alt="AI Secure Logo" 
              className="h-16 w-auto mx-auto mb-2" 
            />
          </div>
          <div>
            <h1 className="text-2xl font-bold font-heading">Créer un compte</h1>
            <p className="text-sm text-muted-foreground mt-1">Rejoignez Nous </p>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Nom complet</Label>
              <Input id="name" placeholder="Jean Dupont" value={name} onChange={(e) => setName(e.target.value)} disabled={isLoading} required />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" placeholder="vous@exemple.com" value={email} onChange={(e) => setEmail(e.target.value)} disabled={isLoading} required />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Mot de passe</Label>
              <div className="relative">
                <Input id="password" type={showPw ? "text" : "password"} placeholder="••••••••" value={password} onChange={(e) => setPassword(e.target.value)} disabled={isLoading} required />
                <button type="button" onClick={() => setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground">
                  {showPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPw">Confirmer le mot de passe</Label>
              <Input id="confirmPw" type="password" placeholder="••••••••" value={confirmPw} onChange={(e) => setConfirmPw(e.target.value)} disabled={isLoading} required />
            </div>
            <div className="space-y-2">
              <Label>Rôle</Label>
              <Select value={role} onValueChange={setRole} disabled={isLoading} required>
                <SelectTrigger><SelectValue placeholder="Sélectionner un rôle" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="ROLE_ANALYSTE">Analyste Sécurité</SelectItem>
                  <SelectItem value="ROLE_ADMIN">Administrateur</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? "Création en cours..." : "Créer un compte"}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Déjà un compte ?{" "}
              <Link to="/login" className="text-primary hover:underline" aria-disabled={isLoading}>Se connecter</Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
