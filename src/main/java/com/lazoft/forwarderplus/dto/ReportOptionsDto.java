package com.lazoft.forwarderplus.dto;

import com.lazoft.forwarderplus.entity.BankDetails;
import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.enums.View;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReportOptionsDto {
    private View view;
    private User user;
    private String hblNo;
    private String consignee;
    private List<User> users;
    private List<BankDetails> bankDetailsList;
    private String reportName;
    private String fileName;
    private String reportSourceFileName;
    private Map<String, Object> parameters;
}
