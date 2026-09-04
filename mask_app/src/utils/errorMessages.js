export const getFriendlyErrorMessage = (status) => {
    const messages = {
        404: 'The requested resource was not found. The server may be misconfigured.',
        500: 'The server encountered an internal error. Please try again later.',
        502: 'The server is temporarily unavailable. This is usually a temporary issue.',
        503: 'The service is currently unavailable. Please try again in a few moments.',
        504: 'The server took too long to respond. Please check your connection.',
    }
    return messages[status] || `An unexpected error occurred (Status: ${status}). Please try again.`
}