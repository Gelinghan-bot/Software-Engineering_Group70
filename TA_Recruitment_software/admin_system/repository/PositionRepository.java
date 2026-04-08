package TA_Recruitment_software.admin_system.repository;

import TA_Recruitment_software.admin_system.foundation.FileStorageUtil;
import TA_Recruitment_software.admin_system.model.Position;
import TA_Recruitment_software.admin_system.model.PositionStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PositionRepository {
    private static final String FILE_NAME = "positions.csv";
    private static final String HEADER =
        "positionId,courseName,responsibleMO,jobDescription,requirements,deadline,workingHours,publishedByUserId,status";

    public List<Position> findAll() {
        List<List<String>> rows = FileStorageUtil.readRows(FILE_NAME, HEADER);
        List<Position> positions = new ArrayList<>();
        for (List<String> row : rows) {
            positions.add(fromRow(row));
        }
        return positions;
    }

    public Optional<Position> findById(String positionId) {
        return findAll().stream().filter(p -> p.getPositionId().equals(positionId)).findFirst();
    }

    public List<Position> findByPublisher(String publisherUserId) {
        List<Position> result = new ArrayList<>();
        for (Position position : findAll()) {
            if (position.getPublishedByUserId().equals(publisherUserId)) {
                result.add(position);
            }
        }
        return result;
    }

    public void save(Position target) {
        List<Position> positions = findAll();
        boolean updated = false;
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i).getPositionId().equals(target.getPositionId())) {
                positions.set(i, target);
                updated = true;
                break;
            }
        }
        if (!updated) {
            positions.add(target);
        }
        saveAll(positions);
    }

    private void saveAll(List<Position> positions) {
        List<List<String>> rows = new ArrayList<>();
        for (Position position : positions) {
            rows.add(toRow(position));
        }
        FileStorageUtil.writeRows(FILE_NAME, HEADER, rows);
    }

    private List<String> toRow(Position position) {
        List<String> row = new ArrayList<>();
        row.add(position.getPositionId());
        row.add(nvl(position.getCourseName()));
        row.add(nvl(position.getResponsibleMO()));
        row.add(nvl(position.getJobDescription()));
        row.add(nvl(position.getRequirements()));
        row.add(nvl(position.getDeadline()));
        row.add(nvl(position.getWorkingHours()));
        row.add(nvl(position.getPublishedByUserId()));
        row.add(position.getStatus().name());
        return row;
    }

    private Position fromRow(List<String> row) {
        Position position = new Position();
        position.setPositionId(cell(row, 0));
        position.setCourseName(cell(row, 1));
        position.setResponsibleMO(cell(row, 2));
        position.setJobDescription(cell(row, 3));
        position.setRequirements(cell(row, 4));
        position.setDeadline(cell(row, 5));
        position.setWorkingHours(cell(row, 6));
        position.setPublishedByUserId(cell(row, 7));
        position.setStatus(PositionStatus.valueOf(cell(row, 8)));
        return position;
    }

    private String cell(List<String> row, int index) {
        if (index >= row.size()) {
            return "";
        }
        return row.get(index);
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}
