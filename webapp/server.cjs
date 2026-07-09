const express = require('express')
const path = require('path')
const http = require('http')
const https = require('https')
const url = require('url')

const app = express()
const PORT = process.env.PORT || 3000
const BACKEND_URL = process.env.BACKEND_URL || 'https://farm-webapp-rezy.onrender.com'

console.log('BACKEND_URL:', BACKEND_URL)

app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true, limit: '10mb' }))

app.use('/api', (req, res) => {
  if (req.url === '/health' || req.url === '/health/') {
    return res.json({ ok: true, backend: BACKEND_URL, timestamp: new Date().toISOString() })
  }

  const targetUrl = new URL(req.url, BACKEND_URL)
  const options = {
    method: req.method,
    hostname: targetUrl.hostname,
    port: targetUrl.port || (targetUrl.protocol === 'https:' ? 443 : 80),
    path: targetUrl.pathname + targetUrl.search,
    headers: {
      ...req.headers,
      host: targetUrl.hostname,
    },
  }

  console.log('[PROXY]', req.method, req.url, '->', targetUrl.toString())

  const proxyReq = (targetUrl.protocol === 'https:' ? https : http).request(options, (proxyRes) => {
    res.writeHead(proxyRes.statusCode || 500, proxyRes.headers)
    proxyRes.pipe(res, { end: true })
  })

  proxyReq.on('error', (err) => {
    console.error('[PROXY ERROR]', err.message)
    res.status(502).send('Proxy error')
  })

  req.pipe(proxyReq, { end: true })
})

app.use(express.static(path.join(__dirname, 'dist')))

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'))
})

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`)
})
