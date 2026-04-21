export function classNames(...values: Array<string | false | null | undefined>) {
  return values.filter(Boolean).join(' ')
}

export function formatDateTime(value: string | null | undefined) {
  if (!value) return '기록 없음'
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

export function formatDate(value: string | null | undefined) {
  if (!value) return '기록 없음'
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}

export function initials(name: string) {
  return name.slice(0, 1)
}

export function avatarTone(seed: string) {
  const tones = [
    ['#ede8fd', '#4c36b0'],
    ['#fdeee8', '#b04c36'],
    ['#e6f5ec', '#2f7d55'],
    ['#e9f0ff', '#3358a6'],
    ['#fff0d9', '#9f6400'],
  ]
  const index = seed.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0) % tones.length
  return {
    background: tones[index][0],
    color: tones[index][1],
  }
}

export async function safe<T>(promise: Promise<T>) {
  try {
    return await promise
  } catch {
    return null
  }
}
