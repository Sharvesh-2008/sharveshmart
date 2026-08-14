export default function EmptyState({ title = 'Nothing here yet', message, children }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-gray-300 bg-white px-6 py-14 text-center">
      <p className="text-lg font-medium text-gray-700">{title}</p>
      {message ? <p className="mt-2 max-w-md text-sm text-gray-500">{message}</p> : null}
      {children ? <div className="mt-4">{children}</div> : null}
    </div>
  )
}