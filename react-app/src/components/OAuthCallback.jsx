// OAuthCallback.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function OAuthCallback() {
  const navigate = useNavigate();
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      const params = new URLSearchParams(window.location.search);
      const code = params.get("code");
      const state = params.get("state");
      const errorParam = params.get("error");

      if (errorParam) {
        setError(errorParam);
        return;
      }
      const savedState = sessionStorage.getItem("pkce_state");
      const codeVerifier = sessionStorage.getItem("pkce_code_verifier");

      if (!code || !state || state !== savedState || !codeVerifier) {
        setError("Invalid PKCE state or missing code_verifier");
        return;
      }

      // Отправляем на backend для обмена и получения своего JWT
      try {
        const resp = await fetch("http://localhost:8081/auth/exchange", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include", // если вы используете cookies; иначе omit
          body: JSON.stringify({
            code,
            code_verifier: codeVerifier
          })
        });

        if (!resp.ok) {
          const txt = await resp.text();
          throw new Error(`Exchange failed: ${resp.status} ${txt}`);
        }
        const data = await resp.json();
        // data: { token: "<app_jwt>", user: { ... } }
        const appToken = data.token;

        // Варианты хранения: localStorage, in-memory, или (лучше) backend Set-Cookie HttpOnly.
        localStorage.setItem("APP_TOKEN", appToken);

        // очистка PKCE данные
        sessionStorage.removeItem("pkce_state");
        sessionStorage.removeItem("pkce_code_verifier");

        // редирект в приложение
        navigate("/");
      } catch (e) {
        console.error(e);
        setError(e.message);
      }
    })();
  }, [navigate]);

  if (error) return <div>OAuth error: {error}</div>;
  return <div>Авторизация... перенаправление.</div>;
}
