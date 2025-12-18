import React, { useEffect, useState } from 'react';
import { User } from '../types';
import { Snackbar, Alert } from '@mui/material';
import SockJS from 'sockjs-client';
import { CompatClient, Stomp } from '@stomp/stompjs';

const WS_URL = '/ws/notifications'; // Asigura-te ca Nginx proxy-uieste spre microserviciu

interface OverconsumptionNotificationProps {
  user: User | null;
}

const OverconsumptionNotification: React.FC<OverconsumptionNotificationProps> = ({ user }) => {
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState('');
  const [client, setClient] = useState<CompatClient | null>(null);

  useEffect(() => {
    if (!user) return;
    const sock = new SockJS(WS_URL);
    const stompClient = Stomp.over(sock);
    stompClient.connect({}, () => {
      stompClient.subscribe('/topic/overconsumption', (msg) => {
        if (msg.body) {
          const notif = JSON.parse(msg.body);
          // Show only if notification is for the logged-in user
          if (notif.personId === user.id) {
            setMessage(`Overconsumption at device ${notif.deviceId}: ${notif.message}`);
            setOpen(true);
          }
        }
      });
    });
    setClient(stompClient);
    return () => {
      stompClient.disconnect();
    };
  }, [user]);

  return (
    <Snackbar open={open} autoHideDuration={6000} onClose={() => setOpen(false)}>
      <Alert onClose={() => setOpen(false)} severity="warning" sx={{ width: '100%' }}>
        {message}
      </Alert>
    </Snackbar>
  );
};

export default OverconsumptionNotification;
