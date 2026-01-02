package model;

import java.util.UUID;

/**
 * 实体: 附件 (Attachment) [cite: 42]
 */
public class Attachment {
    private String attachmentId;
    private byte[] imageData; // (图片附件/票据) [cite: 44]

    public Attachment(byte[] imageData) {
        this.attachmentId = "ATT_" + UUID.randomUUID().toString().substring(0, 8);
        this.imageData = imageData;
    }

    // Getters...
    public String getAttachmentId() { return attachmentId; }
    public byte[] getImageData() { return imageData; }
}