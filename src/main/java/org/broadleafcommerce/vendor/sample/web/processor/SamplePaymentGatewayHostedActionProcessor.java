/*
 * #%L
 * BroadleafCommerce Framework Web
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.broadleafcommerce.vendor.sample.web.processor;

import org.broadleafcommerce.common.payment.dto.PaymentRequestDTO;
import org.broadleafcommerce.common.payment.dto.PaymentResponseDTO;
import org.broadleafcommerce.common.payment.service.PaymentGatewayHostedService;
import org.broadleafcommerce.common.vendor.service.exception.PaymentException;
import org.broadleafcommerce.vendor.sample.service.payment.SamplePaymentGatewayConstants;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import javax.annotation.Resource;

/**
 * <p>A Thymeleaf processor that will generate a Mock Hosted Link given a passed in PaymentRequestDTO.</p>
 *
 * <pre><code>
 * <form blc:sample_payment_hosted_action="${paymentRequestDTO}" complete_checkout="${false}" method="POST">
 *   <input type="image" src="https://www.paypal.com/en_US/i/btn/btn_xpressCheckout.gif" align="left" style="margin-right:7px;" alt="Submit Form" />
 * </form>
 * </code></pre>
 *
 * In order to use this sample processor, you will need to component scan
 * the package "com.broadleafcommerce".
 *
 * This should NOT be used in production, and is meant solely for demonstration
 * purposes only.
 *
 * @author Elbert Bautista (elbertbautista)
 */
@Component("blSamplePaymentGatewayHostedActionProcessor")
public class SamplePaymentGatewayHostedActionProcessor extends AbstractAttributeTagProcessor {

    @Resource(name = "blSamplePaymentGatewayHostedService")
    private PaymentGatewayHostedService paymentGatewayHostedService;

    /**
     * Sets the name of this processor to be used in Thymeleaf template
     */
    public SamplePaymentGatewayHostedActionProcessor() {
        this("sample_payment_hosted_action-size-link");
    }

    protected SamplePaymentGatewayHostedActionProcessor(final String attributeName) {
        super(TemplateMode.HTML, "blc", // prefix for all blc processors
        null, // this is not tag specific
        false, // the tag doesn't have to be prefixed with blc
        attributeName, // the attribute whose value we need to consider
        true, // attribute does need to be prefixed with blc
        10000, // precendence
        true); // we want to remove the attribute
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
        PaymentRequestDTO requestDTO = (PaymentRequestDTO) StandardExpressions.getExpressionParser(context.getConfiguration())
            .parseExpression(context, attributeValue)
            .execute(context);
        String url = "";

        if (requestDTO != null) {
            if (tag.getAttributeValue("complete_checkout") != null) {
                Boolean completeCheckout = (Boolean) StandardExpressions.getExpressionParser(context.getConfiguration())
                    .parseExpression(context, tag.getAttributeValue("complete_checkout"))
                    .execute(context);
                structureHandler.removeAttribute("complete_checkout");
                requestDTO.completeCheckoutOnCallback(completeCheckout);
            }

            try {
                PaymentResponseDTO responseDTO = paymentGatewayHostedService.requestHostedEndpoint(requestDTO);
                url = responseDTO.getResponseMap().get(SamplePaymentGatewayConstants.HOSTED_REDIRECT_URL).toString();
            } catch (PaymentException e) {
                throw new RuntimeException("Unable to Create Sample Payment Gateway Hosted Link", e);
            }
        }
        structureHandler.setAttribute("action", url);
    }
}
