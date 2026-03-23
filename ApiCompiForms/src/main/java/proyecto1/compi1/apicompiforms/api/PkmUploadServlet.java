package proyecto1.compi1.apicompiforms.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "PkmUploadServlet", urlPatterns = {"/api/pkm/upload"})
@MultipartConfig
public class PkmUploadServlet extends BaseApiServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCors(resp);
        req.setCharacterEncoding("UTF-8");

        Part filePart;
        try {
            filePart = req.getPart("file");
        } catch (ServletException ex) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"No se pudo leer multipart\"}");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"Debe enviar un archivo .pkm\"}");
            return;
        }

        String originalName = filePart.getSubmittedFileName();
        String safeName = StorageUtil.sanitizeFileName(originalName);

        try (InputStream input = filePart.getInputStream()) {
            StorageUtil.saveUpload(input, safeName);
        }

        writeJson(
            resp,
            HttpServletResponse.SC_OK,
            "{\"message\":\"Archivo guardado\",\"fileName\":\"" + jsonEscape(safeName) + "\"}"
        );
    }
}
