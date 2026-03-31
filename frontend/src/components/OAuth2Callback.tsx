// Exemple d'intégration frontend OAuth2 avec React

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

const OAuth2Callback = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const handleOAuth2Callback = () => {
      const urlParams = new URLSearchParams(window.location.search);
      const token = urlParams.get('token');
      const provider = urlParams.get('provider');
      const error = urlParams.get('error');

      if (error) {
        setError(`Erreur OAuth2: ${error}`);
        setLoading(false);
        return;
      }

      if (token) {
        // Stocker le token JWT
        localStorage.setItem('jwt_token', token);
        localStorage.setItem('auth_provider', provider);

        // Rediriger vers le dashboard
        navigate('/dashboard');
      } else {
        setError('Token manquant dans la réponse OAuth2');
        setLoading(false);
      }
    };

    handleOAuth2Callback();
  }, [navigate]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">Authentification en cours...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="text-red-500 text-xl mb-4">❌ Erreur d'authentification</div>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={() => navigate('/login')}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            Retour à la connexion
          </button>
        </div>
      </div>
    );
  }

  return null;
};

export default OAuth2Callback;