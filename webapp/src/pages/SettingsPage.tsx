import { useState } from 'react'
import { api } from '../lib/api'
import { PageCard } from '../components/Layout'

export function SettingsPage() {
  const [url, setUrl] = useState('')
  const [message, setMessage] = useState('')

  const handleSave = async () => {
    try {
      await api.post('/api/settings/base-url', { base_url: url })
      setMessage('저장되었습니다.')
    } catch (err: any) {
      setMessage(err.response?.data?.error || '저장에 실패했습니다.')
    }
  }

  return (
    <PageCard title="설정">
      <div className="space-y-4 max-w-xl">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">백엔드 서버 주소</label>
          <input
            type="url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://farm-webapp-rezy.onrender.com/"
            className="w-full px-4 py-3 rounded-xl border border-gray-300 focus:border-green-500 focus:ring-2 focus:ring-green-200 outline-none"
          />
          <p className="text-xs text-gray-500 mt-1">
            변경 시 웹앱은 해당 서버로 API 요청을 볃니다. 비워두면 기본 서버를 사용합니다.
          </p>
        </div>
        {message && <p className="text-sm text-green-700">{message}</p>}
        <button
          onClick={handleSave}
          className="px-6 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800 transition"
        >
          저장
        </button>
      </div>
    </PageCard>
  )
}
