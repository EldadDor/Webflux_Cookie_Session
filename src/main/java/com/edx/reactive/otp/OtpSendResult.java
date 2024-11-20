/*
 * User: nava
 * Date: 14/01/2024
 *
 * Copyright (2005) IDI. All rights reserved.
 * This software is a proprietary information of Israeli Direct Insurance.
 * Created by IntelliJ IDEA.
 */
package com.edx.reactive.otp;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OtpSendResult {
	private String sendMessage;
	private String sendTypeInfo;
	private String clientName;
	private boolean isClientCompanyWorker;

}