// src/routes/AppRouter.jsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from '../pages/Login';
import Dashboard from '../pages/Dashboard';
import CreateAccount from '../pages/CreateAccount';
import Signup from '../pages/Signup';


function AppRouter() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/dashboard" element={<Dashboard />} />

        <Route path="/create-account" element={<CreateAccount />} />
        {/* <Route path="/transfer" element={<Transfer />} />
        <Route path="/scheduled-transfer" element={<ScheduledTransfer />} />
        <Route path="/notifications" element={<Notifications />} /> */}
   
        
      </Routes>
    </Router>
  );
}

export default AppRouter;
