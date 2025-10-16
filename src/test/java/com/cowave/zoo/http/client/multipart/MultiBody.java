package com.cowave.zoo.http.client.multipart;

import lombok.Data;

/**
 *
 * @author shanhuiming
 *
 */
@Data
public class MultiBody {

    private Integer id;

    private String name;

    private String fileName;

    private String fileContent;
}
