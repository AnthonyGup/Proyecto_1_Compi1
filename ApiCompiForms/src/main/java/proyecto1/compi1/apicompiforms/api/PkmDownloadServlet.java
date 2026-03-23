package proyecto1.compi1.apicompiforms.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "PkmDownloadServlet", urlPatterns = {"/api/pkm/download/*"})
public class PkmDownloadServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCors(resp);

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "{\"error\":\"Debe indicar el nombre del archivo\"}");
            return;
        }

        String rawName = URLDecoder.decode(pathInfo.substring(1), StandardCharsets.UTF_8);
        String safeName = StorageUtil.sanitizeFileName(rawName);
        Path target = StorageUtil.resolveTarget(safeName);

        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            writeJson(resp, HttpServletResponse.SC_NOT_FOUND, "{\"error\":\"Archivo no encontrado\"}");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + safeName + "\"");
        resp.setContentLengthLong(Files.size(target));

        try (var input = Files.newInputStream(target); var output = resp.getOutputStream()) {
            input.transferTo(output);
        }
    }
}
