package proyecto1.compi1.apicompiforms.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "PkmFilesServlet", urlPatterns = {"/api/pkm/files"})
public class PkmFilesServlet extends BaseApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCors(resp);

        List<StorageUtil.PkmFileInfo> files = StorageUtil.listPkmFiles();
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < files.size(); i++) {
            StorageUtil.PkmFileInfo file = files.get(i);
            if (i > 0) {
                json.append(',');
            }

            json.append('{')
                .append("\"fileName\":\"").append(jsonEscape(file.fileName)).append("\",")
                .append("\"createdAt\":\"").append(jsonEscape(file.createdAt)).append("\",")
                .append("\"sizeBytes\":").append(file.sizeBytes)
                .append('}');
        }

        json.append("]");
        writeJson(resp, HttpServletResponse.SC_OK, json.toString());
    }
}
