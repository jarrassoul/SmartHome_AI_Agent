import { useState, useRef, useEffect } from 'react'
import { sendMessage, confirmAction } from './api'
import './App.css'

function App() {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [requiresConfirmation, setRequiresConfirmation] = useState(false)
  const [isEditing, setIsEditing] = useState(false)
  const messagesEndRef = useRef(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const handleSend = async () => {
    if (!input.trim()) return

    const userMessage = input.trim()
    setMessages(prev => [...prev, { type: 'user', text: userMessage }])
    setInput('')
    setLoading(true)

    try {
      const response = await sendMessage(userMessage)
      setMessages(prev => [...prev, { type: 'assistant', text: response.message }])
      setRequiresConfirmation(response.requiresConfirmation)
    } catch (error) {
      setMessages(prev => [...prev, { type: 'error', text: 'Error: ' + error.message }])
    } finally {
      setLoading(false)
    }
  }

  const handleConfirm = async (action) => {
    if (action === 'edit') {
      setIsEditing(true)
      setRequiresConfirmation(false)
      setInput('')
      return
    }

    setLoading(true)
    try {
      const response = await confirmAction(action)
      setMessages(prev => [...prev, { type: 'assistant', text: response.message }])
      setRequiresConfirmation(response.requiresConfirmation)
      setIsEditing(false)
    } catch (error) {
      setMessages(prev => [...prev, { type: 'error', text: 'Error: ' + error.message }])
    } finally {
      setLoading(false)
    }
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className="app">
      <header className="header">
        <h1>Smart Home AI Agent</h1>
      </header>

      <div className="chat-container">
        <div className="messages">
          {messages.length === 0 && (
            <div className="welcome">
              <h2>Welcome to Smart Home AI!</h2>
              <p>Try commands like:</p>
              <ul>
                <li>"Turn on the lights"</li>
                <li>"Set temperature to 22 degrees"</li>
                <li>"Lock all doors"</li>
                <li>"Activate morning routine"</li>
              </ul>
            </div>
          )}
          {messages.map((msg, index) => (
            <div key={index} className={`message ${msg.type}`}>
              <div className="message-content">{msg.text}</div>
            </div>
          ))}
          {loading && (
            <div className="message assistant">
              <div className="message-content loading">Thinking...</div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {requiresConfirmation && (
          <div className="confirmation">
            <p>Do you want to proceed?</p>
            <button onClick={() => handleConfirm('confirm')} className="btn-confirm">Yes</button>
            <button onClick={() => handleConfirm('cancel')} className="btn-cancel">No</button>
            <button onClick={() => handleConfirm('edit')} className="btn-edit">Edit</button>
          </div>
        )}

        {isEditing && (
          <div className="editing-hint">
            <p>Type your modified command below and press Send</p>
          </div>
        )}

        <div className="input-area">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Type your command..."
            disabled={loading}
          />
          <button onClick={handleSend} disabled={loading || !input.trim()}>
            Send
          </button>
        </div>
      </div>
    </div>
  )
}

export default App
