/** Tamaño máximo de imagen aceptado (se guarda como base64 en la BD). */
export const MAX_IMAGEN_BYTES = 2 * 1024 * 1024; // 2 MB

/**
 * Lee un fichero de imagen y lo devuelve como data URL base64
 * (p. ej. "data:image/jpeg;base64,…"), listo para guardar/mostrar.
 * Rechaza si no es imagen o si supera el tamaño máximo.
 */
export function imagenADataUrl(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    if (!file.type.startsWith('image/')) {
      reject(new Error('El archivo debe ser una imagen.'));
      return;
    }
    if (file.size > MAX_IMAGEN_BYTES) {
      reject(new Error('La imagen no puede superar los 2 MB.'));
      return;
    }
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = () => reject(new Error('No se pudo leer la imagen.'));
    reader.readAsDataURL(file);
  });
}
