package es.ual.dra.agrotrack.service;

import es.ual.dra.agrotrack.model.entity.AlertaPrecio;
import es.ual.dra.agrotrack.model.entity.Producto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificacionService {

    private final JavaMailSender mailSender;

    public void enviarAlertaPrecio(AlertaPrecio alerta, BigDecimal precioActual, Producto producto) {
        if (!alerta.getUsuario().isNotificacionesEmail()) return;

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(alerta.getUsuario().getEmail());
            mensaje.setSubject(String.format(
                "[AgroTrack] Alerta de precio: %s ha superado tu umbral",
                producto.getNombre()
            ));
            mensaje.setText(String.format(
                """
                Hola %s,

                El precio mayorista de %s ha superado tu umbral configurado.

                  • Precio actual (media de mercados): %.2f €/%s
                  • Tu umbral:                        %.2f €/%s

                Consulta el historial completo y el análisis del asistente IA en AgroTrack:
                http://localhost

                ──
                AgroTrack · Seguimiento de Cultivos y Precios Mayoristas
                Para desactivar esta alerta, accede a tu perfil en la aplicación.
                """,
                alerta.getUsuario().getNombre(),
                producto.getNombre(),
                precioActual, producto.getUnidad(),
                alerta.getPrecioUmbral(), producto.getUnidad()
            ));
            mensaje.setFrom("noreply@agrotrack.local");
            mailSender.send(mensaje);
            log.info("Email de alerta enviado a {}", alerta.getUsuario().getEmail());
        } catch (Exception e) {
            log.error("Error enviando email de alerta a {}: {}",
                alerta.getUsuario().getEmail(), e.getMessage());
        }
    }
}
