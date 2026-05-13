import { useState, useEffect } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { Eye, EyeOff, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { toast } from "sonner";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [showConfirmPw, setShowConfirmPw] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  
  const navigate = useNavigate();

  useEffect(() => {
    if (!token) {
      toast.error("Token de réinitialisation manquant ou invalide.");
      navigate("/login");
    }
  }, [token, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (password !== confirmPassword) {
      toast.error("Les mots de passe ne correspondent pas.");
      return;
    }
    
    // Validation regex as required by the backend
    const passwordRegex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$/;
    if (!passwordRegex.test(password)) {
      toast.error("Le mot de passe doit contenir au moins 8 caractères, un chiffre, une majuscule, une minuscule et un caractère spécial.");
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch("/api/auth/reset-password", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ token, newPassword: password }),
      });

      const data = await response.json();

      if (response.ok) {
        setIsSuccess(true);
        toast.success(data.message || "Mot de passe réinitialisé avec succès.");
      } else {
        toast.error(data.error || "Erreur lors de la réinitialisation.");
      }
    } catch (error) {
      toast.error("Erreur serveur inattendue");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <Card className="w-full max-w-md border-border/50 shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-primary to-accent"></div>
        <CardHeader className="text-center space-y-4 pb-2 mt-2">
          <div className="mx-auto w-fit">
            <img 
              src="/logo_AIsecure2-removebg-preview.png" 
              alt="AI Secure Logo" 
              className="h-16 w-auto mx-auto mb-2" 
            />
          </div>
          <div>
            <h1 className="text-2xl font-bold font-heading african-heading mx-auto w-fit">Nouveau mot de passe</h1>
            <p className="text-sm text-muted-foreground mt-1">
              Veuillez entrer votre nouveau mot de passe
            </p>
          </div>
        </CardHeader>
        <CardContent>
          {!isSuccess ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="password">Nouveau mot de passe</Label>
                <div className="relative">
                  <Input 
                    id="password" 
                    type={showPw ? "text" : "password"} 
                    placeholder="••••••••" 
                    value={password} 
                    onChange={(e) => setPassword(e.target.value)} 
                    disabled={isLoading} 
                    required 
                    className="focus-visible:ring-primary/50" 
                  />
                  <button type="button" onClick={() => setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground">
                    {showPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirmer le mot de passe</Label>
                <div className="relative">
                  <Input 
                    id="confirmPassword" 
                    type={showConfirmPw ? "text" : "password"} 
                    placeholder="••••••••" 
                    value={confirmPassword} 
                    onChange={(e) => setConfirmPassword(e.target.value)} 
                    disabled={isLoading} 
                    required 
                    className="focus-visible:ring-primary/50" 
                  />
                  <button type="button" onClick={() => setShowConfirmPw(!showConfirmPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground">
                    {showConfirmPw ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
              </div>
              
              <Button type="submit" className="w-full btn-african" disabled={isLoading}>
                {isLoading ? "Réinitialisation..." : "Réinitialiser le mot de passe"}
              </Button>
            </form>
          ) : (
            <div className="text-center space-y-4 py-4">
              <div className="bg-green-500/10 text-green-600 dark:text-green-400 p-4 rounded-lg">
                Votre mot de passe a été réinitialisé avec succès. Vous pouvez maintenant vous connecter.
              </div>
              <Button asChild className="w-full btn-african">
                <Link to="/login">Se connecter</Link>
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
