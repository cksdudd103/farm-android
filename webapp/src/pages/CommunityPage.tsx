import { useEffect, useState } from 'react'
import {
  fetchPosts,
  fetchPostDetail,
  createPost,
  updatePost,
  deletePost,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import { useAuth } from '../contexts/AuthContext'
import type { Post } from '../types/api'
import { Pin, Eye, Search } from 'lucide-react'

const categories = ['전체', '공지', '자유', '질문', '판매', '정보공유']
const editableCategories = ['자유', '질문', '판매', '정보공유']

export function CommunityPage() {
  const { user } = useAuth()
  const [posts, setPosts] = useState<Post[]>([])
  const [pinned, setPinned] = useState<Post[]>([])
  const [category, setCategory] = useState('전체')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(1)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selected, setSelected] = useState<Post | null>(null)
  const [editing, setEditing] = useState<Post | null>(null)
  const [isOpen, setIsOpen] = useState(false)

  const load = () => {
    setLoading(true)
    setError('')
    fetchPosts({ category: category === '전체' ? undefined : category, q: search || undefined, page })
      .then((res) => {
        setPosts(res.data || [])
        setPinned(res.pinned || [])
        setTotalPages(res.total_pages || 1)
      })
      .catch((err) => setError(err?.response?.data?.msg || err.message || '게시글을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [category, page])

  const handleSearch = () => {
    setPage(1)
    load()
  }

  const openDetail = async (p: Post) => {
    try {
      const detail = await fetchPostDetail(p.id)
      setSelected(detail)
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '게시글을 불러오지 못했습니다.')
    }
  }

  const canManage = (p: Post) => user?.role === 'admin' || p.user_id === user?.id

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const fd = new FormData(e.currentTarget)
    try {
      if (editing) {
        await updatePost(editing.id, fd)
      } else {
        await createPost(fd)
      }
      setIsOpen(false)
      setEditing(null)
      setSelected(null)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '저장에 실패했습니다.')
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    try {
      await deletePost(id)
      setSelected(null)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '삭제에 실패했습니다.')
    }
  }

  return (
    <PageCard title="커뮤니티 게시판">
      <div className="flex flex-col md:flex-row gap-3 mb-4">
        <select value={category} onChange={(e) => { setCategory(e.target.value); setPage(1) }} className="px-3 py-2 border rounded-lg">
          {categories.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            placeholder="제목, 내용 검색"
            className="w-full pl-9 pr-3 py-2 border rounded-lg"
          />
        </div>
        <button onClick={handleSearch} className="px-4 py-2 border rounded-lg hover:bg-gray-50">검색</button>
        <button onClick={() => { setEditing(null); setIsOpen(true) }} className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800">
          글쓰기
        </button>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {loading ? (
        <div className="py-10 text-center text-gray-500">불러오는 중...</div>
      ) : posts.length === 0 && pinned.length === 0 ? (
        <p className="text-gray-500">등록된 게시글이 없습니다.</p>
      ) : (
        <>
          <ul className="divide-y divide-gray-200">
            {[...pinned, ...posts].map((p) => (
              <li key={p.id} onClick={() => openDetail(p)} className="py-3 flex items-center justify-between cursor-pointer hover:bg-gray-50 px-2 rounded-lg">
                <div className="flex items-center gap-2 min-w-0">
                  {p.is_pinned && <Pin className="w-4 h-4 text-red-500 shrink-0" />}
                  <span className="text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-700 shrink-0">{p.category}</span>
                  <p className="font-medium text-gray-900 truncate">{p.title}</p>
                </div>
                <div className="flex items-center gap-3 text-xs text-gray-500 shrink-0 ml-2">
                  <span>{p.author_name}</span>
                  <span>{p.created_date}</span>
                  <span className="flex items-center gap-1"><Eye className="w-3 h-3" />{p.views}</span>
                </div>
              </li>
            ))}
          </ul>

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-4">
              {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
                <button
                  key={p}
                  onClick={() => setPage(p)}
                  className={`w-8 h-8 rounded-lg text-sm ${p === page ? 'bg-green-700 text-white' : 'border hover:bg-gray-50'}`}
                >
                  {p}
                </button>
              ))}
            </div>
          )}
        </>
      )}

      {selected && (
        <PostDetailModal
          post={selected}
          canManage={canManage(selected)}
          onClose={() => setSelected(null)}
          onEdit={() => { setEditing(selected); setSelected(null); setIsOpen(true) }}
          onDelete={() => handleDelete(selected.id)}
        />
      )}

      {isOpen && (
        <PostModal
          post={editing}
          onClose={() => { setIsOpen(false); setEditing(null) }}
          onSubmit={handleSubmit}
        />
      )}
    </PageCard>
  )
}

function PostDetailModal({
  post,
  canManage,
  onClose,
  onEdit,
  onDelete,
}: {
  post: Post
  canManage: boolean
  onClose: () => void
  onEdit: () => void
  onDelete: () => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center gap-2 mb-2">
          <span className="text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-700">{post.category}</span>
          {post.is_pinned && <Pin className="w-4 h-4 text-red-500" />}
        </div>
        <h2 className="text-xl font-bold mb-2">{post.title}</h2>
        <div className="flex items-center gap-3 text-xs text-gray-500 mb-4">
          <span>{post.author_name}</span>
          <span>{post.created_at}</span>
          <span className="flex items-center gap-1"><Eye className="w-3 h-3" />{post.views}</span>
        </div>
        {post.image && <img src={post.image} alt="첨부이미지" className="w-full rounded-lg mb-4" />}
        <p className="text-gray-700 whitespace-pre-wrap">{post.content}</p>

        <div className="flex gap-2 pt-6">
          {canManage && (
            <>
              <button onClick={onEdit} className="flex-1 py-2 border rounded-lg">수정</button>
              <button onClick={onDelete} className="flex-1 py-2 border border-red-300 text-red-600 rounded-lg">삭제</button>
            </>
          )}
          <button onClick={onClose} className="flex-1 py-2 bg-green-700 text-white rounded-lg">닫기</button>
        </div>
      </div>
    </div>
  )
}

function PostModal({
  post,
  onClose,
  onSubmit,
}: {
  post: Post | null
  onClose: () => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">{post ? '게시글 수정' : '게시글 작성'}</h2>
        <form onSubmit={onSubmit} className="space-y-3" encType="multipart/form-data">
          <select name="category" defaultValue={post?.category || '자유'} className="w-full px-3 py-2 border rounded-lg">
            {editableCategories.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
          <input name="title" defaultValue={post?.title || ''} placeholder="제목" className="w-full px-3 py-2 border rounded-lg" required />
          <textarea name="content" defaultValue={post?.content || ''} placeholder="내용" className="w-full px-3 py-2 border rounded-lg" rows={6} required />
          <input name="image" type="file" accept="image/*" className="w-full text-sm" />

          <div className="flex gap-2 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg">취소</button>
            <button type="submit" className="flex-1 py-2 bg-green-700 text-white rounded-lg">저장</button>
          </div>
        </form>
      </div>
    </div>
  )
}
