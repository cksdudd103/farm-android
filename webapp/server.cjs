const express = require('express')
const { createProxyMiddleware } = require('http-proxy-middleware')
const path = require('path')

const app = express()
const PORT = process.env.PORT || 3000
const BACKEND_URL = process.env.BACKEND_URL || 'https://farm-webapp-rezy.onrender.com'

app.use('/api', createProxyMiddleware({
  target: BACKEND_URL,
  changeOrigin: true,
}))

app.use(express.static(path.join(__dirname, 'dist')))

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'))
})

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`)
})
