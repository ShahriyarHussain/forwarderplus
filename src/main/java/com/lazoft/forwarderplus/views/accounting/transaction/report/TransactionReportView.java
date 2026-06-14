package com.lazoft.forwarderplus.views.accounting.transaction.report;

import com.lazoft.forwarderplus.entity.finance.MetabaseReport;
import com.lazoft.forwarderplus.repository.finance.MetabaseReportRepository;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@PageTitle("View Transactions")
@Route(value = "transaction-report", layout = MainLayout.class)
@RolesAllowed({"ADMIN", "FINANCE"})
@Slf4j
public class TransactionReportView extends VerticalLayout {

    @Value("${metabase.base.url}")
    private String metabaseBaseUrl;

    @Value("${metabase.secret.key}")
    private String metabaseSecretKey;

    private final MetabaseReportRepository metabaseReportRepository;

    private final ComboBox<MetabaseReport> reportNameComboBox = new ComboBox<>();
    private Button showButton = new Button("Preview");
    private IFrame iframe = new IFrame();

    public TransactionReportView(MetabaseReportRepository reportRepository) {
        metabaseReportRepository = reportRepository;
        reportNameComboBox.setItems(metabaseReportRepository.findAll());
        reportNameComboBox.setItemLabelGenerator(MetabaseReport::getReportName);
        reportNameComboBox.setWidth("20%");
        showButtonAction();
        iframe.setVisible(false);
        HorizontalLayout layout = new HorizontalLayout();
        layout.add(reportNameComboBox, showButton);
        layout.setWidth("100%");
        add(layout, new Hr(), iframe);
    }

    public void showButtonAction() {
        showButton.addClickListener(event -> {
            iframe.setVisible(true);
            iframe.setWidth("100%");
            iframe.setHeight("80vh");

            String jwtToken = generateJwtToken(metabaseSecretKey, reportNameComboBox.getValue().getReportId());
            String encodedJwtToken = URLEncoder.encode(jwtToken, StandardCharsets.UTF_8);
            String secureUrl = metabaseBaseUrl + "/embed/question/" + encodedJwtToken;
            iframe.setSrc(secureUrl);
        });
    }

    private String generateJwtToken(String secretKey, long questionId) {
        Map<String, Object> resource = new HashMap<>();
        resource.put("question", questionId);
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> claims = new HashMap<>();
        claims.put("resource", resource);
        claims.put("params", params);
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 5. Build and sign the JWT
        return Jwts.builder()
                .setClaims(claims) // <-- This is the crucial missing piece
                .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Expires in 24 hours
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
