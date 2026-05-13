import { useState } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { toast } from "sonner";
import { ArrowLeft } from "lucide-react";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSent, setIsSent] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const response = await fetch("/api/auth/forgot-password", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email }),
      });

      if (response.ok) {
        setIsSent(true);
        toast.success("Si un compte est associé à cet email, un lien a été envoyé.");
      } else {
        toast.error("Une erreur est survenue.");
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
            <h1 className="text-2xl font-bold font-heading african-heading mx-auto w-fit">Mot de passe oublié</h1>
            <p className="text-sm text-muted-foreground mt-1">
              Entrez votre email pour recevoir un lien de réinitialisation
            </p>
          </div>
        </CardHeader>
        <CardContent>
          {!isSent ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="Entrez votre email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled={isLoading}
                  required
                  className="focus-visible:ring-primary/50"
                />
              </div>

              <Button type="submit" className="w-full btn-african" disabled={isLoading}>
                {isLoading ? "Envoi en cours..." : "Envoyer le lien"}
              </Button>
              <div className="text-center mt-4">
                <Link to="/login" className="text-sm text-primary flex items-center justify-center hover:underline">
                  <ArrowLeft className="h-4 w-4 mr-1" /> Retour à la connexion
                </Link>
              </div>
            </form>
          ) : (
            <div className="text-center space-y-4 py-4">
              <div className="bg-primary/10 text-primary p-4 rounded-lg">
                Un email a été envoyé si cette adresse existe dans notre système. Veuillez vérifier votre boîte de réception.
              </div>
              <Button asChild className="w-full btn-african">
                <Link to="/login">Retour à la connexion</Link>
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
