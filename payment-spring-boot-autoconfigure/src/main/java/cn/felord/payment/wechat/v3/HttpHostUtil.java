package cn.felord.payment.wechat.v3;

import lombok.Setter;
import org.apache.http.HttpHost;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpHostUtil {

    @Setter
    private static volatile String profilesActive;
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
            String active = profilesActive;
            if (active == null) {
                active = getActive();
            }
            String applicationYmlName = "application-" + active + ".yml";
            loadConfig(applicationYmlName);
            httpProxy = getConfigValue(key, String.class);
            if (Objects.nonNull(httpProxy)) {
                CONFIG_MAP.put(key, httpProxy);
            }
        }
        return Objects.nonNull(httpProxy) ? HttpHost.create((String) httpProxy) : null;
    }

    /**
     * 优先获取JVM中的参数，如果存在则直接返回，不存在则从application.yml中获取
     * @return String
     */
    private String getActive() {
        // -Dspring.profiles.active 获取
        String active = System.getProperty("spring.profiles.active");
        if (active != null) {
            return active;
        }
        // 从application.yml 获取
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
