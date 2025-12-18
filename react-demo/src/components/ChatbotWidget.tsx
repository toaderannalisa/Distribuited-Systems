import React, { useState } from 'react';
import { Box, IconButton, TextField, Button, Paper, Typography, Collapse } from '@mui/material';
import ChatIcon from '@mui/icons-material/Chat';
import CloseIcon from '@mui/icons-material/Close';

const API_URL = '/api/chatbot/ask'; // Asigura-te ca Nginx proxy-uieste spre microserviciu

const ChatbotWidget: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<{from: string, text: string}[]>([]);
  const [loading, setLoading] = useState(false);

  const handleSend = async () => {
    if (!input.trim()) return;
    setMessages(msgs => [...msgs, {from: 'user', text: input}]);
    setLoading(true);
    try {
      const res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: input })
      });
      const data = await res.json();
      setMessages(msgs => [...msgs, {from: 'bot', text: data.answer}]);
    } catch (e) {
      setMessages(msgs => [...msgs, {from: 'bot', text: 'Error contacting support.'}]);
    }
    setInput('');
    setLoading(false);
  };

  return (
    <Box sx={{ position: 'fixed', bottom: 24, right: 24, zIndex: 2000 }}>
      {!open && (
        <IconButton color="primary" onClick={() => setOpen(true)} size="large">
          <ChatIcon fontSize="large" />
        </IconButton>
      )}
      <Collapse in={open}>
        <Paper elevation={6} sx={{ width: 320, p: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">Support Chatbot</Typography>
            <IconButton size="small" onClick={() => setOpen(false)}><CloseIcon /></IconButton>
          </Box>
          <Box sx={{ flex: 1, minHeight: 120, maxHeight: 200, overflowY: 'auto', mb: 1 }}>
            {messages.map((msg, idx) => (
              <Box key={idx} sx={{ textAlign: msg.from === 'user' ? 'right' : 'left', mb: 0.5 }}>
                <Typography variant="body2" color={msg.from === 'user' ? 'primary' : 'secondary'}>
                  {msg.text}
                </Typography>
              </Box>
            ))}
          </Box>
          <Box display="flex" gap={1}>
            <TextField
              size="small"
              fullWidth
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter') handleSend(); }}
              disabled={loading}
              placeholder="Ask something..."
            />
            <Button variant="contained" onClick={handleSend} disabled={loading || !input.trim()}>Send</Button>
          </Box>
        </Paper>
      </Collapse>
    </Box>
  );
};

export default ChatbotWidget;
