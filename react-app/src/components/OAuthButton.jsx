// OAuthButton.jsx
import React from "react";
import { generateRandomString, generateCodeChallenge } from "./pkceUtils";

export default function OAuthButton({ provider = "google" }) {
  // Настройки (подгоните)
  const clientId = "844450274439-ess0u5v4nk2j567me7qsodf8spr7344h.apps.googleusercontent.com";
  const redirectUri = "http://localhost:3000/oauth2/callback"; // ваш redirect в SPA
  const scope = "openid email profile";
  const authorizationEndpoint = "https://accounts.google.com/o/oauth2/v2/auth";

  const handleClick = async () => {
    const codeVerifier = generateRandomString(80);
    const state = generateRandomString(16);

    // сохраняем в sessionStorage временно
    sessionStorage.setItem("pkce_code_verifier", codeVerifier);
    sessionStorage.setItem("pkce_state", state);

    const codeChallenge = await generateCodeChallenge(codeVerifier);

    const params = new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri,
      response_type: "code",
      scope: scope,
      state: state,
      code_challenge: codeChallenge,
      code_challenge_method: "S256",
      // prompt: "consent" // опционально
    });

    // редирект браузера на провайдера
    window.location.href = `${authorizationEndpoint}?${params.toString()}`;
  };

  return (
    <button onClick={handleClick}>
      Войти через Google (PKCE)
    </button>
  );
}
