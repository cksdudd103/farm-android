self.addEventListener('install', () => {
  self.skipWaiting()
})

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim())
})

// Minimal pass-through fetch handler. Required for PWA installability on
// most browsers, but intentionally does not cache API responses so data
// always stays fresh.
self.addEventListener('fetch', (event) => {
  event.respondWith(fetch(event.request).catch(() => caches.match(event.request)))
})
