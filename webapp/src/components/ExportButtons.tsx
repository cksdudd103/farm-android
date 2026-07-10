import { FileDown, FileText } from 'lucide-react'

interface ExportButtonsProps {
  data: Record<string, any>[]
  filename: string
  title?: string
}

function toCSV(rows: Record<string, any>[]): string {
  if (rows.length === 0) return ''
  const headers = Object.keys(rows[0])
  const lines = [
    headers.join(','),
    ...rows.map((row) =>
      headers
        .map((h) => {
          const value = row[h]
          if (value === null || value === undefined) return ''
          const text = String(value).replace(/"/g, '""')
          return text.includes(',') || text.includes('\n') || text.includes('"') ? `"${text}"` : text
        })
        .join(',')
    ),
  ]
  return '\uFEFF' + lines.join('\n')
}

function downloadFile(content: string, filename: string, type: string) {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

export function ExportButtons({ data, filename, title }: ExportButtonsProps) {
  const handleCSV = () => {
    const csv = toCSV(data)
    downloadFile(csv, `${filename}.csv`, 'text/csv;charset=utf-8')
  }

  const handlePDF = async () => {
    const { jsPDF } = await import('jspdf')
    const doc = new jsPDF({ orientation: 'p', unit: 'mm', format: 'a4' })
    doc.setFontSize(16)
    doc.text(title || filename, 14, 20)
    doc.setFontSize(10)

    const headers = data.length > 0 ? Object.keys(data[0]) : []
    const colWidth = headers.length > 0 ? 180 / headers.length : 180
    let y = 30

    // header
    doc.setFillColor(230, 230, 230)
    doc.rect(14, y - 5, 180, 7, 'F')
    headers.forEach((h, i) => {
      doc.text(String(h), 14 + i * colWidth, y)
    })
    y += 8

    data.forEach((row) => {
      if (y > 270) {
        doc.addPage()
        y = 20
      }
      headers.forEach((h, i) => {
        const text = row[h] === null || row[h] === undefined ? '' : String(row[h]).slice(0, 30)
        doc.text(text, 14 + i * colWidth, y)
      })
      y += 7
    })

    doc.save(`${filename}.pdf`)
  }

  return (
    <div className="flex gap-2">
      <button
        onClick={handleCSV}
        className="inline-flex items-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
      >
        <FileDown className="w-4 h-4" /> CSV
      </button>
      <button
        onClick={handlePDF}
        className="inline-flex items-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
      >
        <FileText className="w-4 h-4" /> PDF
      </button>
    </div>
  )
}
