package org.springframework.feign.codec;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Data
public class RemoteChainHolder {

    private final String holderName;

    private ArrayList<RemoteChain> chains;
}
