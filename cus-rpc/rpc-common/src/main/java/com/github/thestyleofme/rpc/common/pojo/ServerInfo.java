package com.github.thestyleofme.rpc.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * description
 * </p>
 *
 * @author isaac 2020/10/26 17:01
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerInfo {

    private String serverName;
    private String ipAndPort;
    private String lastDateTime;
    private Long cost;
}
