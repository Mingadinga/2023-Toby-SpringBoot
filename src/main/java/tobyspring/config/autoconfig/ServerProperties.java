package tobyspring.config.autoconfig;

import tobyspring.config.MyConfigurationProperties;

@MyConfigurationProperties(prefix = "server") // 프로퍼티의 네임스페이스
public class ServerProperties {

    private String contextPath;

    private int port;

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
