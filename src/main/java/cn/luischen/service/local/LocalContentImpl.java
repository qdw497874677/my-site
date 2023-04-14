package cn.luischen.service.local;

import cn.luischen.constant.Types;
import cn.luischen.dao.ContentDao;
import cn.luischen.model.ContentDomain;
import cn.luischen.service.content.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static cn.luischen.utils.Commons.closeQuietly;
import static cn.luischen.utils.FileUtils.loadEndWithFileContentMap;
import static cn.luischen.utils.FileUtils.loadEndWithFileList;

/**
 *
 * @author qdw
 */
@Service
public class LocalContentImpl implements LocalContent {

    @Autowired
    private ContentDao contentDao;
    @Autowired
    private ContentService contentService;

    private static final String TAG_FLAG = "自动同步";

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refresh() {
        // 查看配置
        FileInputStream inputStream = null;
        Set<String> targetSet = new HashSet<>();
        try {
            List<File> configFiles = loadEndWithFileList("doc/content", "config.properties");
            if (!CollectionUtils.isEmpty(configFiles)) {
                Properties properties = new Properties();
                File file = configFiles.get(0);
                inputStream = new FileInputStream(file);
                properties.load(inputStream);
                String target = new String(properties.getProperty("target").getBytes( StandardCharsets.ISO_8859_1)
                        , StandardCharsets.UTF_8);
                if (!StringUtils.isEmpty(target)) {
                    String[] split = target.split(",");
                    targetSet.addAll(Arrays.asList(split));
                }
            }
        } catch (Exception e) {
        } finally {
            closeQuietly(inputStream);
        }

        Map<String, List<String>> stringListMap = loadEndWithFileContentMap("doc/content", ".md");
        if (stringListMap.isEmpty()) {
            return;
        }
        // 刷新所有本地导入文档
        contentDao.deleteArticleByTag(TAG_FLAG);
        for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
            String key = entry.getKey();
            if (!CollectionUtils.isEmpty(targetSet) && !targetSet.contains(key)) {
                continue;
            }
            ContentDomain contentDomain = new ContentDomain();
            contentDomain.setAllowComment(1);
            contentDomain.setStatus("publish");
            // 标记这些文档是本地导入的
            contentDomain.setTags(TAG_FLAG);
            contentDomain.setType(Types.ARTICLE.getType());
            contentDomain.setTitle(key);
            StringBuilder stringBuilder = new StringBuilder();
            List<String> value = entry.getValue();
            if (CollectionUtils.isEmpty(value)) {
                continue;
            }
            for (String s : value) {
                stringBuilder.append(s).append("\n");
            }
            contentDomain.setContent(stringBuilder.toString());
            contentService.addArticle(contentDomain);
        }
    }
}
