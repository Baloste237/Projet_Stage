import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { Eye, EyeOff } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { toast } from "sonner";

export default function Login() {
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const success = await login(identifier, password);
      if (success) {
        toast.success("Connexion réussie!");
        navigate("/");
      } else {
        toast.error("Identifiants ou mot de passe incorrects");
      }
    } catch (error) {
      toast.error("Erreur serveur inattendue");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md border-border/50">
        <CardHeader className="text-center space-y-4 pb-2">
          <div className="mx-auto w-fit">
            <img 
              src="/logo_AIsecure2-removebg-preview.png" 
              alt="AI Secure Logo" 
              className="h-16 w-auto mx-auto mb-2" 
            />
          </div>
          <div>
            <h1 className="text-2xl font-bold font-heading">AiSecureScan</h1>
            <p className="text-sm text-muted-foreground mt-1">Connectez-vous à votre compte</p>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="identifier">Nom d'utilisateur ou Email</Label>
              <Input id="identifier" type="text" placeholder="Entrez votre nom ou email" value={identifier} onChange={(e) => setIdentifier(e.target.value)} disabled={isLoading} required />
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
            <div className="text-right">
              <a href="#" className="text-xs text-primary hover:underline">Mot de passe oublié ?</a>
            </div>
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? "Connexion en cours..." : "Connexion"}
            </Button>
            <p className="text-center text-sm text-muted-foreground">
              Pas de compte ?{" "}
              <Link to="/register" className="text-primary hover:underline" aria-disabled={isLoading}>Créer un compte</Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
