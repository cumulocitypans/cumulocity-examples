package c8y.trackeragent.operations;

import c8y.CommunicationMode;
import c8y.Mobile;
import c8y.trackeragent.configuration.TrackerConfiguration;
import c8y.trackeragent.devicebootstrap.DeviceCredentialsRepository;
import c8y.trackeragent.sms.OptionsAuthorizationSupplier;
import com.cumulocity.model.idtype.GId;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.rest.representation.operation.OperationRepresentation;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.cumulocity.sms.client.SmsMessagingApi;
import com.cumulocity.sms.client.model.Address;
import com.cumulocity.sms.client.model.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.cumulocity.sms.client.model.Address.phoneNumber;


@Component
public class OperationSmsDelivery {

    private final InventoryApi inventoryApi;
    private final SmsMessagingApi outgoingMessagingClient;

    private final DeviceCredentialsRepository deviceCredentials;
    private final OptionsAuthorizationSupplier optionsAuthSupplier;

    @Autowired
    public OperationSmsDelivery (
            InventoryApi inventoryApi,
            SmsMessagingApi outgoingMessagingClient,
            TrackerConfiguration config,
            DeviceCredentialsRepository deviceCredentials,
            OptionsAuthorizationSupplier optionsAuthSupplier) {
        this.inventoryApi = inventoryApi;
        this.outgoingMessagingClient = outgoingMessagingClient;
        this.deviceCredentials = deviceCredentials;
        this.optionsAuthSupplier = optionsAuthSupplier;
    }
    
    /**
     * return Operation delivery mode based on "Device control via SMS" chapter link: http://cumulocity.com/guides/reference/device-control/
     * "c8y_CommunicationMode": {
        "mode": "SMS"
        }
        or
        "deliveryType": "SMS"
     * @param operation
     * @return operation delivery Mode
     */
    public boolean isSmsMode (OperationRepresentation operation) {
        
        if ("sms".equalsIgnoreCase(String.valueOf(operation.get("deliveryType")))) {
            return true;
        } 

        ManagedObjectRepresentation deviceMo = inventoryApi.get(operation.getDeviceId());
        CommunicationMode communicationMode = deviceMo.get(CommunicationMode.class);

        if (communicationMode != null && "sms".equalsIgnoreCase(communicationMode.getMode())) {
            return true;
        }
        
        return false;
    }

    public void deliverSms (String translation, GId deviceId, String imei) throws IllegalArgumentException {

        ManagedObjectRepresentation deviceMo = inventoryApi.get(deviceId);
        Mobile mobile = deviceMo.get(Mobile.class);
        String receiver = mobile.getMsisdn();
        
        if (receiver == null || receiver.length() == 0) {
            throw new IllegalArgumentException("MSISDN of target device cannot be null");
        }

        final Address address = phoneNumber(receiver);
        final SendMessageRequest request = SendMessageRequest.builder().withReceiver(address).withSender(address).withMessage(translation).build();
        
        setOptionsReaderAuthForTenant(imei);
        outgoingMessagingClient.sendMessage(request);

    }

    private void setOptionsReaderAuthForTenant(String imei) {
        String tenant = deviceCredentials.getDeviceCredentials(imei).getTenant();
        optionsAuthSupplier.optionsAuthForTenant(tenant);
    }
}
