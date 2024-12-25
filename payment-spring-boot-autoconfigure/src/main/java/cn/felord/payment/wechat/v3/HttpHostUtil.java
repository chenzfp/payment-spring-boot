package cn.felord.payment.wechat.v3;

import org.apache.http.HttpHost;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpHostUtil {

    // 1. 定义一个私有静态的实例变量
    private static volatile HttpHostUtil instance;

    // 2. 私有化构造函数
    private HttpHostUtil() {
    }

    // 3. 提供一个公共的静态方法来获取实例
    public static HttpHostUtil getInstance() {
        if (instance == null) {
            synchronized (HttpHostUtil.class) {
                if (instance == null) {
                    instance = new HttpHostUtil();
                }
            }
        }
        return instance;
    }

    public HttpHost getProxy() {
        String key = "wechat.httpProxy";
        Object httpProxy = CONFIG_MAP.get(key);
        // 配置文件仅读取一次
        if (CONFIG_MAP.isEmpty()) {
            String active = getActive();
            String applicationYmlName = "application-" + active + ".yml";
            loadConfig(applicationYmlName);
            httpProxy = getConfigValue(key, String.class);
            CONFIG_MAP.put(key, httpProxy);
        }
        return Objects.nonNull(httpProxy) ? HttpHost.create((String) httpProxy) : null;
    }

    private String getActive() {
        loadConfig("application.yml");
        return getConfigValue("spring.profiles.active", String.class);
    }

    private static final Map<String, Object> CONFIG_MAP = new HashMap<>();

    private void loadConfig(String ymlFileName) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(ymlFileName);
        if (inputStream == null) {
            throw new RuntimeException("无法找到配置文件: " + ymlFileName);
        }
        Yaml yaml = new Yaml();
        Map<String, Object> tempMap = yaml.load(inputStream);
        if (tempMap != null) {
            CONFIG_MAP.putAll(tempMap);
        }
    }

    public Object getConfigValue(String key) {
        String[] keys = key.split("\\.");
        Object current = CONFIG_MAP;
        for (String k : keys) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<?, ?>) current).get(k);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key, Class<T> targetType) {
        Object value = getConfigValue(key);
        if (targetType.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

}
