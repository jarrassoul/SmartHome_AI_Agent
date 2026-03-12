const API_BASE_URL = 'http://localhost:8080/api';

export const sendMessage = async (message, userId = 'webUser') => {
  const response = await fetch(`${API_BASE_URL}/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ message, userId }),
  });

  if (!response.ok) {
    throw new Error('Failed to send message');
  }

  return response.json();
};

export const confirmAction = async (action, userId = 'webUser') => {
  const response = await fetch(`${API_BASE_URL}/confirm`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ action, userId }),
  });

  if (!response.ok) {
    throw new Error('Failed to confirm action');
  }

  return response.json();
};

export const getStatus = async () => {
  const response = await fetch(`${API_BASE_URL}/status`);

  if (!response.ok) {
    throw new Error('Failed to get status');
  }

  return response.json();
};
