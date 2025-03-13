import React from "react";
import OAuthButton from "./OAuthButton";
import OAuthButtonGithub from "./OAuthButtonGithub";

const Home = () => {

    const googleLogin = () => {
        window.location.href = 'http://localhost:8081/oauth2/authorization/google'
    };
    const githubLogin = () => {
        window.location.href = 'http://localhost:8081/oauth2/authorization/github'
    };

    return (
        <div>
            <div>
            <h2>Welcome to the Oauth2 Demo</h2>
            <button onClick={googleLogin}>Login with Google</button>
            <button onClick={githubLogin}>Login with Github</button>
        </div>
        
        <div style={{ maxWidth: 420, margin: "40px auto", textAlign: "center" }}>
      <h1>Вход в приложение</h1>
      <OAuthButton provider="google" />
      <OAuthButtonGithub provider="github" />
    </div>
        </div>
        
    );
}

export default Home;