package com.froobworld.nabsuite.modules.admin.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import com.froobworld.nabsuite.command.NabCommand;
import com.froobworld.nabsuite.command.argument.arguments.PageNumberArgument;
import com.froobworld.nabsuite.modules.admin.AdminModule;
import com.froobworld.nabsuite.modules.admin.tasks.StaffTask;
import com.froobworld.nabsuite.util.ListPaginator;
import com.froobworld.nabsuite.util.NumberDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class StaffTasksCommand extends NabCommand {
    private static final int ITEMS_PER_PAGE = 5;
    private final AdminModule adminModule;

    public StaffTasksCommand(AdminModule adminModule) {
        super(
                "stafftasks",
                "Get a list of staff tasks that require action.",
                "nabsuite.command.stafftasks",
                CommandSender.class
        );
        this.adminModule = adminModule;
    }

    @Override
    public void execute(CommandContext<CommandSender> context) {
        List<StaffTask> staffTasks = adminModule.getStaffTaskManager().getStaffTasks(context.getSender());

        if (staffTasks.isEmpty()) {
            context.getSender().sendMessage(Component.text("There are no staff tasks.").color(NamedTextColor.YELLOW));
        } else {
            int pageNumber = context.get("page");
            List<StaffTask>[] pages = ListPaginator.paginate(staffTasks, ITEMS_PER_PAGE);
            List<StaffTask> page = pages[pageNumber - 1];
            context.getSender().sendMessage(
                    Component.text("There " + NumberDisplayer.toStringWithModifierAndPrefix(staffTasks.size(), " task", " tasks", "is ", "are ") + " requiring action. ")
                            .append(Component.text("Showing page " + pageNumber + "/" + pages.length + ".")).color(NamedTextColor.YELLOW)
            );
            for (StaffTask staffTask : page) {
                context.getSender().sendMessage(
                        Component.text("- ").color(NamedTextColor.GOLD)
                                .append(staffTask.getTaskMessage())
                );
            }
        }
    }

    @Override
    public Command.Builder<CommandSender> populateBuilder(Command.Builder<CommandSender> builder) {
        return builder
                .argument(
                        new PageNumberArgument<>(
                                false,
                                "page",
                                context -> adminModule.getStaffTaskManager().getStaffTasks(context.getSender()).size(),
                                ITEMS_PER_PAGE
                        ),
                        ArgumentDescription.of("page")
                );
    }

}
