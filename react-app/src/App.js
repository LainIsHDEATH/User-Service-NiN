import './App.css';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Home from './components/Home'
import OAuthCallback from './components/OAuthCallback';

function App() {
  return (
    // <div className="App">
    //  <Home/>
    // </div>
    <Router>
      <Routes>
        <Route path='/' element={<Home/>}/>
        <Route path='/dashboard' element={<Home/>}/>
        <Route path='/oauth2/callback' element={<OAuthCallback/>}/>
      </Routes>
    </Router>
  );
}

export default App;
 