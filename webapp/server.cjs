const express = require('express')
const path = require('path')
const fetch = require('node-fetch')

const app = express()
const PORT = process.env.PORT || 3000
const BACKEND_URL = (process.env.BACKEND_URL || 'https://farm-webapp-rezy.onrender.com/api').replace(/\/$/, '')

console.log('BACKEND_URL:', BACKEND_URL)

app.use('/api', async (req, res) => {
  if (req.url === '/health' || req.url === '/health/') {
    return res.json({ ok: true, backend: BACKEND_URL, timestamp: new Date().toISOString() })
  }

  const targetUrl = BACKEND_URL + req.url
  console.log('[PROXY]', req.method, req.url, '->', targetUrl)

  try {
    const chunks = []
    for await (const chunk of req) {
      chunks.push(chunk)
    }
    const body = Buffer.concat(chunks)

    const headers = {}
    Object.entries(req.headers).forEach(([key, value]) => {
      if (key !== 'host' && key !== 'content-length') {
        headers[key] = value
      }
    })
    headers.host = new URL(BACKEND_URL).hostname
    if (body.length > 0) {
      headers['content-length'] = String(body.length)
    }

    const response = await fetch(targetUrl, {
      method: req.method,
      headers,
      body: body.length > 0 ? body : undefined,
      redirect: 'manual',
      credentials: 'include',
    })

    res.status(response.status)
    response.headers.forEach((value, key) => {
      if (key === 'set-cookie') {
        res.setHeader('set-cookie', response.headers.raw()['set-cookie'])
      } else if (key !== 'content-encoding' && key !== 'transfer-encoding') {
        res.setHeader(key, value)
      }
    })
    response.body.pipe(res)
  } catch (err) {
    console.error('[PROXY ERROR]', err.message)
    res.status(502).json({ ok: false, error: 'Proxy error: ' + err.message })
  }
})

app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true, limit: '10mb' }))

app.use(express.static(path.join(__dirname, 'dist')))

app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'))
})

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`)
})
