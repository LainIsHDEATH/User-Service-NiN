// OAuthButtonGithub.jsx
import React from "react";
import { generateRandomString, generateCodeChallenge } from "./pkceUtils";

export default function OAuthButtonGithub({ provider = "github" }) {
  // передавайте clientId и redirectUri извне (env)
  const clientId = "Ov23lib9UtwdmWgU39XL";
  const redirectUri = "http://localhost:3000/oauth2/callback"; // ваш redirect в SPA
  const scope = "read:user user:email";
  const authorizationEndpoint = "https://github.com/login/oauth/authorize";

  const handleClick = async () => {
    const codeVerifier = generateRandomString(80);
    const state = generateRandomString(16);

    sessionStorage.setItem("pkce_code_verifier", codeVerifier);
    sessionStorage.setItem("pkce_state", state);

    const codeChallenge = await generateCodeChallenge(codeVerifier);

    const params = new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri,
      scope: scope, // read profile + emails
      state: state,
      code_challenge: codeChallenge,
      code_challenge_method: "S256",
      allow_signup: "true"
    });

    window.location.href = `${authorizationEndpoint}?${params.toString()}`;
  };

  return (
    <button onClick={handleClick}>
      Войти через GitHub (PKCE)
    </button>
  );
}