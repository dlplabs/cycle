'use client';

import React, { useEffect, useCallback } from 'react';

interface QuickExitButtonProps {
  redirectUrl?: string;
  onBeforeExit?: () => void;
  className?: string;
}

export const QuickExitButton: React.FC<QuickExitButtonProps> = ({
  redirectUrl = 'https://www.google.com.br',
  onBeforeExit,
  className = '',
}) => {
  const triggerQuickExit = useCallback(() => {
    if (onBeforeExit) {
      try {
        onBeforeExit();
      } catch (_e) {
        // Ignora erros para garantir redirecionamento imediato
      }
    }
    // Invalida a entrada no histórico do navegador para impedir o botão Voltar
    window.location.replace(redirectUrl);
  }, [onBeforeExit, redirectUrl]);

  useEffect(() => {
    let escCount = 0;
    let timer: NodeJS.Timeout;

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        escCount++;
        clearTimeout(timer);
        if (escCount >= 3) {
          triggerQuickExit();
        } else {
          timer = setTimeout(() => {
            escCount = 0;
          }, 1000);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      clearTimeout(timer);
    };
  }, [triggerQuickExit]);

  return (
    <button
      type="button"
      onClick={triggerQuickExit}
      aria-label="Sair Imediatamente do site por segurança"
      className={`fixed top-4 right-4 z-50 flex items-center gap-2 rounded-full bg-red-600 px-4 py-2.5 text-sm font-bold text-white shadow-lg transition-transform hover:scale-105 active:scale-95 ${className}`}
    >
      <svg
        className="h-4 w-4"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          android-id="exit-path"
          d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
        />
      </svg>
      <span>Sair Rápido (Esc x3)</span>
    </button>
  );
};

export default QuickExitButton;
