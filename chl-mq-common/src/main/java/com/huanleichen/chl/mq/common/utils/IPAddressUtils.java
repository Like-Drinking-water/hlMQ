package com.huanleichen.chl.mq.common.utils;

import cn.hutool.core.util.StrUtil;
import com.huanleichen.chl.mq.common.exception.IPAddressParseException;
import io.netty.channel.Channel;

public class IPAddressUtils {

    public static String[] getIPAndPort(String address) {
        if (address == null || StrUtil.isBlank(address)) {
            throw new IPAddressParseException();
        }

        String[] ipAndPort = address.split(":");

        if (ipAndPort.length != 2) {
            throw new IPAddressParseException();
        }
        return ipAndPort;
    }

    public static String parseAddress(final Channel channel) {
        if (channel == null) {
            return "";
        }
        String remoteAddress = channel.remoteAddress() == null ? "" : channel.remoteAddress().toString();
        if (remoteAddress.length() > 0) {
            int index = remoteAddress.lastIndexOf('/');
            if (index >= 0) {
                return remoteAddress.substring(index + 1);
            }

            return remoteAddress;
        }
        return "";

    }
}
